package test.example.com.uestcer.presenter.impl;

import android.util.Log;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import java.util.ArrayList;
import java.util.List;

import test.example.com.uestcer.callback.MyEMCallBack;
import test.example.com.uestcer.presenter.ChatPresenter;
import test.example.com.uestcer.utils.ThreadUtils;
import test.example.com.uestcer.view.ChatView;

/**
 * Created by oo on 2017/7/6.
 */

public class ChatPresenterImpl implements ChatPresenter{
    private ChatView chatView;
    public ChatPresenterImpl(ChatView chatView){
        this.chatView=chatView;
    }
    //获取到的消息
    private List<EMMessage> messages=new ArrayList<>();

    /**
     * 获取历史消息
     * @param username 使用者的username
     * @return 获取到的与该联系人所有的消息
     */
    @Override
    public List<EMMessage> getChatHistoryMessage(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        if (conversation!=null){
            //设置所有消息为已读,点进去了就设置所有消息为已读
            conversation.markAllMessagesAsRead();
            //从环信的数据库中获取历史消息
            EMMessage lastMessage = conversation.getLastMessage();
            //最近一条消息的id
            String msgId = lastMessage.getMsgId();
            //从最近一条数据开始，所有的消息数目，不包括最后一条
            int allMsgCount = conversation.getAllMsgCount();
            //loadMoreMsgFromDB第一个参数:从哪个ID号开始，第二个参数:获取多少条
            //获取到的emMessages不包括最近一条
            List<EMMessage> emMessages = conversation.loadMoreMsgFromDB(msgId, allMsgCount);
            //清空数据库
            messages.clear();
            messages.addAll(emMessages);
            messages.add(lastMessage);
            //打印历史聊天记录  调试信息
            Log.i("getChatHistoryMessage", messages.toString());
            //测试历史聊天记录逻辑没问题，View层有问题
            chatView.onGetHistoryMessage(messages);
            //
        }
        return messages;
    }

    @Override
    public void sendMessage(final String msgText, String contact) {
        final EMMessage message = EMMessage.createTxtSendMessage(msgText, contact);
        messages.add(message);

        //消息发送状态的监听
        message.setMessageStatusCallback(new MyEMCallBack() {
            @Override
            public void success() {

                //发送成功或者失败都更新chatView
                chatView.updateList();
                Log.i("send successful", msgText);
            }

            @Override
            public void Error(int i, String s) {
                //发送失败
                chatView.updateList();
            }
        });
        ThreadUtils.runOnNonUIThread(new Runnable() {
            @Override
            public void run() {
                EMClient.getInstance().chatManager().sendMessage(message);
                Log.i("run", "run: "+message.toString());
            }
        });
    }
}
