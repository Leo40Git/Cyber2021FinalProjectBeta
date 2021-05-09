package edu.kfirawad.cyber2021finalprojectbeta.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import edu.kfirawad.cyber2021finalprojectbeta.R;

public class StringInputDialogFragment extends DialogFragment {
    public interface Listener {
        void onStringInputComplete(DialogFragment dialog, String input);
        void onStringInputCancelled(DialogFragment dialog);
    }

    private final int hintResId;
    private final CharSequence hintCharSeq;
    private Listener listener;

    public StringInputDialogFragment(int hintResId) {
        this.hintResId = hintResId;
        hintCharSeq = null;
    }

    public StringInputDialogFragment(CharSequence hintCharSeq) {
        this.hintCharSeq = hintCharSeq;
        hintResId = View.NO_ID;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement StringInputDialogFragment.Listener");
        }
    }

    @Override
    public @NonNull Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_string_input, null);
        EditText etRideName = view.findViewById(R.id.etStringInput);
        if (hintCharSeq != null)
            etRideName.setHint(hintCharSeq);
        else if (hintResId > View.NO_ID)
            etRideName.setHint(hintResId);
        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String rideName = etRideName.getText().toString();
                    listener.onStringInputComplete(this, rideName);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) ->
                        listener.onStringInputCancelled(this))
                .create();
    }
}
