package com.centrifugal.centrifuge.android.test;

/**
 * This file is part of ACentrifugo.
 * <p/>
 * ACentrifugo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * ACentrifugo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ACentrifugo.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
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
