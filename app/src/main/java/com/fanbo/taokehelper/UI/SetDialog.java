package com.fanbo.taokehelper.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.fanbo.taokehelper.R;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Administrator on 2018/1/7.
 */

public class SetDialog extends AlertDialog {
   private EditText ET_content,ET_time;
    private Button btn_sure;
   public interface OnSetSureListener{
       void onSetListener(String content,String time);
   }

    public void setOnSetSureListener(OnSetSureListener onSetSureListener) {
        this.onSetSureListener = onSetSureListener;
    }

    private OnSetSureListener onSetSureListener ;
    public SetDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_dialog);
        ET_content =(EditText) findViewById(R.id.et_content);
        ET_time =(EditText) findViewById(R.id.et_time);

        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        btn_sure =(Button) findViewById(R.id.btn_setsure);
        initView();
    }

    private void initView() {
        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onSetSureListener!=null){
                    onSetSureListener.onSetListener(ET_content.getText().toString(),ET_time.getText().toString());
                }
                dismiss();
            }
        });


    }

    public SetDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected SetDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }
}
