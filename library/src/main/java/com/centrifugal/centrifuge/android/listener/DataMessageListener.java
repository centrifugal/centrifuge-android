package com.centrifugal.centrifuge.android.listener;

import com.centrifugal.centrifuge.android.message.DataMessage;

/**
 * This file is part of ACentrifugo.
 *
 * ACentrifugo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ACentrifugo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ACentrifugo.  If not, see See <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * Created by Semyon on 01.05.2016.
 * */
public interface DataMessageListener {

    void onNewDataMessage(final DataMessage message);

}
