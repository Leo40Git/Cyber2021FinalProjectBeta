package edu.kfirawad.cyber2021finalprojectbeta.fragment;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.kfirawad.cyber2021finalprojectbeta.R;

public final class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView tvTitle, tvDesc;
    private Runnable onClickListener;

    public ListViewHolder(@NonNull View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tvTitle);
        tvDesc = itemView.findViewById(R.id.tvDesc);
        tvTitle.setOnClickListener(this);
        tvDesc.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (onClickListener != null)
            onClickListener.run();
    }

    public void setTitle(@NonNull CharSequence title) {
        tvTitle.setText(title);
    }

    public void setDescription(@NonNull CharSequence desc) {
        tvDesc.setText(desc);
    }

    public void setOnClickListener(Runnable onClickListener) {
        this.onClickListener = onClickListener;
    }
}
