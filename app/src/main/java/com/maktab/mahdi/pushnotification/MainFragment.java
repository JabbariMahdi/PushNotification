package com.maktab.mahdi.pushnotification;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import co.ronash.pushe.Pushe;

import static android.content.Context.MODE_PRIVATE;


public class MainFragment extends Fragment {

    public static String MY_PREFS_NAME = "pushPreferences";
    public static boolean active = false;
    private String phonePid = "pid_ade2-4e61-3b";
    private String emulatorPid = "pid_92f8-cf6a-35";
    private Chip phoneChip;
    private Chip emulatorChip;
    private Button sendButton;
    private EditText editText;
    private TextView messageTextView;
    private String currentPid;
    private String senderName;
    private String message;
    private TextView senderTextView;
    private LinearLayout linearLayout;
    private ImageView deleteButton;
    private SharedPreferences prefs;
    private ScrollView scrollView;


    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Pushe.isPusheInitialized(getActivity())) {
            Log.i("pid", Pushe.getPusheId(getActivity()));
            currentPid = Pushe.getPusheId(getActivity());
        }
        prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        phoneChip = view.findViewById(R.id.phone_chip);
        emulatorChip = view.findViewById(R.id.emulator_chip);
        sendButton = view.findViewById(R.id.send_button);
        messageTextView = view.findViewById(R.id.text_view);
        editText = view.findViewById(R.id.edit_text);
        senderTextView = view.findViewById(R.id.sender_txt);
        linearLayout = view.findViewById(R.id.linear_layout);
        deleteButton = view.findViewById(R.id.delete);
        scrollView = view.findViewById(R.id.scroll_view);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText() == null || editText.getText().length() == 0) {
                    editText.setError("Please enter a text");
                    return;
                }
                if (phoneChip.isChecked())
                    sendPush(phonePid, currentPid.equals(phonePid) ? "Phone" : "Emulator", editText.getText().toString());
                if (emulatorChip.isChecked())
                    sendPush(emulatorPid, currentPid.equals(phonePid) ? "Phone" : "Emulator", editText.getText().toString());
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                senderName = null;
                message = null;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("sender", senderName);
                editor.putString("message", message);
                editor.apply();
                updateUi();
            }
        });

        return view;
    }

    private void sendPush(String pid, String senderName, String message) {
        if (Pushe.isPusheInitialized(getActivity())) {
            try {
                Pushe.sendCustomJsonToUser(getActivity(), pid
                        , "{ \"sender\":\"" + senderName + "\", \"message\":\"" + message + "\" }");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        senderName = prefs.getString("sender", "");
        message = prefs.getString("message", "");
        updateUi();
    }

    private void updateUi() {
        senderTextView.setText(senderName);
        messageTextView.setText(message);
        editText.setText("");
        scrollView.smoothScrollTo(0, scrollView.getBottom());
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        senderName = event.getSenderName();
        message = event.getMessage();
        Snackbar.make(linearLayout, R.string.new_message, Snackbar.LENGTH_LONG).show();
        updateUi();
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
        EventBus.getDefault().register(this);
    }


    @Override
    public void onStop() {
        active = false;
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
