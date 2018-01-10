package com.fanbo.taokehelper.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fanbo.taokehelper.Adapter.DissGroupAdapter;
import com.fanbo.taokehelper.Adapter.FriendAdapter;
import com.fanbo.taokehelper.Adapter.GroupAdapter;
import com.fanbo.taokehelper.R;
import com.fanbo.taokehelper.UI.QQLIstView;
import com.fanbo.taokehelper.UI.SetDialog;
import com.fanbo.taokehelper.Utils.StringUtil;
import com.scienjus.smartqq.client.QQClient;
import com.scienjus.smartqq.model.Discuss;
import com.scienjus.smartqq.model.Friend;
import com.scienjus.smartqq.model.Group;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QQMessageActivity extends AppCompatActivity {

    @BindView(R.id.message_toolbar)
    Toolbar messageToolbar;
    @BindView(R.id.cb_firend)
    CheckBox cbFirend;
    @BindView(R.id.ll_firend)
    LinearLayout llFirend;
    @BindView(R.id.list_friend)
    QQLIstView listFriend;
    @BindView(R.id.cb_Group)
    CheckBox cbGroup;
    @BindView(R.id.qqlist_qroup)
    QQLIstView qqlistQroup;
    @BindView(R.id.cb_dissGroup)
    CheckBox cbDissGroup;
    @BindView(R.id.qqlist_dissGroup)
    QQLIstView qqlistDissGroup;
    @BindView(R.id.tv_friendNum)
    TextView tvFriendNum;
    @BindView(R.id.tv_GroupNum)
    TextView tvGroupNum;
    @BindView(R.id.tv_dissGroupNum)
    TextView tvDissGroupNum;
    @BindView(R.id.ll_group)
    LinearLayout llGroup;
    @BindView(R.id.ll_dissGroup)
    LinearLayout llDissGroup;
    @BindView(R.id.tv_sendnum)
    TextView tvSendnum;
    @BindView(R.id.ll_sendBtn)
    LinearLayout llSendBtn;
    @BindView(R.id.tv_send)
    TextView tvSend;
    private List<Friend> shouldSendFriend;
    private List<Group> shouldSendGroup;
    private List<Discuss> shouldSendDisCuss;
    private FriendAdapter friendAdapter;
    private GroupAdapter groupAdapter;
    private DissGroupAdapter dissGroupAdapter;
    private static String send_time;
    private static String send_content;
    private static final String LOG = "QQMessageActivity";
    Handler friendhander = new Handler();
    Handler grouphandler = new Handler();
    Handler dissGrouphandler =new Handler();
    private static boolean isStartSend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qqmessage);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initData() {
        shouldSendDisCuss = new ArrayList<>();
        shouldSendFriend = new ArrayList<>();
        shouldSendGroup = new ArrayList<>();
        getFriendData();

        getGroupData();

        getDissGroupData();

        setTitleMenu();

        llSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStartSend == false) {
                    setSendData();
                    tvSend.setText("停止");
                } else {
                    friendhander.removeCallbacks(friendRunnable);
                    grouphandler.removeCallbacks(groupRunnable);
                    dissGrouphandler.removeCallbacks(dissGroupRunnable);
                    isStartSend = false;
                    tvSend.setText("发射");
                }
            }
        });
    }

    private void setTitleMenu() {
        messageToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_set) {
                    SetDialog setDialog = new SetDialog(QQMessageActivity.this);
                    setDialog.show();
                    setDialog.setOnSetSureListener(new SetDialog.OnSetSureListener() {
                        @Override
                        public void onSetListener(String content, String time) {
                            send_time = time;
                            send_content = content;
                        }
                    });
                }
                return false;
            }
        });
    }

    private void getDissGroupData() {
        QQClient.getInstance().getDiscussList(new QQClient.Listener() {
            @Override
            public void success(Object object) {
                final List<Discuss> dissGroups = (List<Discuss>) object;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dissGroupAdapter == null) {
                            tvDissGroupNum.setText(String.valueOf(dissGroups.size()));
                            dissGroupAdapter = new DissGroupAdapter(QQMessageActivity.this, dissGroups);
                            qqlistDissGroup.setAdapter(dissGroupAdapter);
                            qqlistDissGroup.setVisibility(View.GONE);
                        } else {
                            dissGroupAdapter.notifyDataSetChanged();
                        }
                        dissGroupAdapter.setOnDissGroupLIstener(new DissGroupAdapter.OnDissGroupListener() {
                            @Override
                            public void DissChooseListener(List<Discuss> discussList) {
                                shouldSendDisCuss =discussList ;
                                tvSendnum.setText(String.valueOf(shouldSendDisCuss.size()+shouldSendGroup.size()+shouldSendFriend.size()));
                            }
                        });
                    }
                });

            }

            @Override
            public void fail(int code, String msg) {
                Log.d(LOG, "获取讨论群失败" + msg);
            }
        });

        llDissGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qqlistDissGroup.isShown() == true) {
                    qqlistDissGroup.setVisibility(View.GONE);
                } else {
                    qqlistDissGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getGroupData() {
        QQClient.getInstance().getGroupList(new QQClient.Listener() {
            @Override
            public void success(Object object) {
                final List<Group> groups = (List<Group>) object;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (groupAdapter == null) {
                            tvGroupNum.setText(String.valueOf(groups.size()));
                            groupAdapter = new GroupAdapter(QQMessageActivity.this, groups);
                            qqlistQroup.setAdapter(groupAdapter);
                            qqlistQroup.setVisibility(View.GONE);
                        } else {
                            groupAdapter.notifyDataSetChanged();
                        }
                        groupAdapter.setGroupChooseListener(new GroupAdapter.GroupChooseListener() {
                            @Override
                            public void ChooseGroupListener(List<Group> groups) {
                                shouldSendGroup = groups;
                                tvSendnum.setText(String.valueOf(shouldSendDisCuss.size()+shouldSendGroup.size()+shouldSendFriend.size()));
                            }
                        });

                    }
                });

            }

            @Override
            public void fail(int code, String msg) {
                Log.d(LOG, "获取群失败" + msg);
            }
        });
        llGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qqlistQroup.isShown() == true) {
                    qqlistQroup.setVisibility(View.GONE);
                } else {
                    qqlistQroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getFriendData() {
        //获取好友列表
        QQClient.getInstance().getFriendList(new QQClient.Listener() {
            @Override
            public void success(Object object) {
                final List<Friend> friends = (List<Friend>) object;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvFriendNum.setText(String.valueOf(friends.size()));
                        if (friendAdapter == null) {
                            friendAdapter = new FriendAdapter(QQMessageActivity.this, friends);
                            listFriend.setAdapter(friendAdapter);
                            listFriend.setVisibility(View.GONE);
                        } else {
                            friendAdapter.notifyDataSetChanged();
                        }
                        friendAdapter.setOnChooseListener(new FriendAdapter.OnChooseListener() {
                            @Override
                            public void CBChooseListener(List<Friend> friendList) {
                                shouldSendFriend = friendList;
                                tvSendnum.setText(String.valueOf(shouldSendDisCuss.size()+shouldSendGroup.size()+shouldSendFriend.size()));
                            }
                        });
                    }
                });
            }

            @Override
            public void fail(int code, String msg) {
                Log.d(LOG, "获取好友失败" + msg);
            }
        });


        llFirend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listFriend.isShown() == true) {
                    listFriend.setVisibility(View.GONE);
                } else {
                    listFriend.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void setSendData() {
        if (StringUtil.isNotEmpty(send_content, true) == false) {
            Toast.makeText(this, "发送内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (StringUtil.isNotEmpty(send_time, true) == false) {
            Toast.makeText(this, "发送时间不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        isStartSend = true;
        int int_time = Integer.parseInt(send_time);
        if (shouldSendFriend.size() > 0) {
            friendhander.postDelayed(friendRunnable, int_time);
        }
        if (shouldSendGroup.size() > 0) {
            grouphandler.postDelayed(groupRunnable,int_time);
        }
        if (shouldSendDisCuss.size()>0){
            dissGrouphandler.postDelayed(dissGroupRunnable,int_time);
        }
    }
    Runnable friendRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(LOG, "发送消息");
            for (int i = 0; i < shouldSendFriend.size(); i++) {
                QQClient.getInstance().sendMessageToFriend(shouldSendFriend.get(i).getUserId(), send_content);
            }
            friendhander.postDelayed(this, Integer.parseInt(send_time));
        }
    };
    Runnable groupRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i <shouldSendGroup.size() ; i++) {
                QQClient.getInstance().sendMessageToGroup(shouldSendGroup.get(0).getId(), send_content);
            }
            grouphandler.postDelayed(this,Integer.parseInt(send_time));
        }
    };
    Runnable dissGroupRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i <shouldSendDisCuss.size() ; i++) {
                QQClient.getInstance().sendMessageToDiscuss(shouldSendDisCuss.get(0).getId(), send_content);
            }
            dissGrouphandler.postDelayed(this,Integer.parseInt(send_time));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return true;
    }

    private void initView() {
        setSupportActionBar(messageToolbar);
    }
}
