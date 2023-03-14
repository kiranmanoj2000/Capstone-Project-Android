package com.dev.capstoneapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dev.capstoneapp.R;
import com.dev.capstoneapp.models.InputMethod;

import java.util.ArrayList;

public class InputMethodAdapter extends ArrayAdapter<InputMethod> {

    public InputMethodAdapter(Context context, ArrayList<InputMethod> algorithmList)
    {
        super(context, 0, algorithmList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable
            View convertView, @NonNull ViewGroup parent)
    {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView,
                          ViewGroup parent)
    {
        // It is used to set our custom view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.input_method_spinner_item, parent, false);
        }

        TextView textViewName = convertView.findViewById(R.id.input_method_text_view);
        InputMethod currentItem = getItem(position);

        // It is used the name to the TextView when the
        // current item is not null.
        if (currentItem != null) {
            textViewName.setText(currentItem.getLabel());
        }
        return convertView;
    }
}