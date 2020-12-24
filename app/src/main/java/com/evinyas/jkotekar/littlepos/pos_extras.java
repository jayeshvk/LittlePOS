package com.evinyas.jkotekar.littlepos;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.DialogFragment;

public class pos_extras extends DialogFragment {

    private ImageButton save;
    private extrasDataFragment mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialogTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.pos_extras, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setTitle("Extras");
        save = (ImageButton) rootView.findViewById(R.id.pos_extras_save);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        save.setOnClickListener(new View.OnClickListener() {
            EditText surcharges = (EditText) getView().findViewById(R.id.pos_surcharges);

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentInteraction(surcharges.getText().toString());
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof extrasDataFragment) {
            mListener = (extrasDataFragment) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement extrasDataFragment");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface extrasDataFragment {
        void onFragmentInteraction(String surcharges);
    }

/*    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }*/
}
