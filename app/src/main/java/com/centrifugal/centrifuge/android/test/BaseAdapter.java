package com.centrifugal.centrifuge.android.test;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 08.05.2016.
 */
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Semyon on 02.12.2014.
 */
public abstract class BaseAdapter<H extends BaseAdapter.BaseHolder, T> extends ArrayAdapter<T> {

    public BaseAdapter(final Context context, final int resource) {
        super(context, resource);
    }

    public BaseAdapter(final Context context, final int resource, final int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public BaseAdapter(final Context context, final int resource, final T[] objects) {
        super(context, resource, objects);
    }

    public BaseAdapter(final Context context, final int resource, final int textViewResourceId, final T[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public BaseAdapter(final Context context, final int resource, final List<T> objects) {
        super(context, resource, objects);
    }

    public BaseAdapter(final Context context, final int resource, final int textViewResourceId, final List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public final View getView(final int position, final View convertView, final ViewGroup parent) {
        H holder = null;
        if (convertView == null) {
            holder = onCreateViewHolder(parent, position);
        } else {
            Object tag = convertView.getTag();
            if (!(tag instanceof BaseHolder)) {
                holder = onCreateViewHolder(parent, position);
            } else {
                holder = (H) tag;
            }
        }
        onBindViewHolder(holder, position);
        return holder.view;
    }

    public abstract void onBindViewHolder(final H holder, final int position);

    public abstract H onCreateViewHolder(final ViewGroup viewGroup, final int position);

    public static class BaseHolder {

        View view;

        protected BaseHolder(final View view) {
            view.setTag(this);
            this.view = view;
        }

        protected <T> T findViewById(final int id) {
            return (T) view.findViewById(id);
        }

    }

}
