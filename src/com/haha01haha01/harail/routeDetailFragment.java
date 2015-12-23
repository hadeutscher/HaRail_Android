/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A fragment representing a single route detail screen. This fragment is either
 * contained in a {@link routeListActivity} in two-pane mode (on tablets) or a
 * {@link routeDetailActivity} on handsets.
 */
public class routeDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The train this fragment is presenting.
	 */
	private int mTrainId;

	private Activity parent;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public routeDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mTrainId = getArguments().getInt(ARG_ITEM_ID);
			parent.setTitle("Train #" + Integer.toString(mTrainId));
		}
	}
	
	// Android API is shit, we need to use different overrides before and after API23

	@TargetApi(23)
	@Override
	public void onAttach(Context context) {
	super.onAttach(context);
		if (context instanceof Activity) {
			handleOnAttach((Activity)context);
		} 
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		handleOnAttach(activity);
	}

	private void handleOnAttach(Activity activity) {
		parent = activity;
	}

	private List<String> decodeData(int[] path) {
		List<String> result = new ArrayList<String>();
		int i = 0;
		switch (path[i++]) {
		case 0:
			result.add(HaRailAPI.getLastError());
			break;
		case 1:
			int train_count = path[i++];
			int last_dest_id = -1;
			int last_dest_time = -1;
			for (int j = 0; j < train_count; j++) {
				int source_id = path[i++];
				int source_time = path[i++];
				int dest_id = path[i++];
				int dest_time = path[i++];
				if (last_dest_time == -1 || last_dest_time == source_time) {
					result.add(Utils.stationsById.get(source_id) + " ("
							+ Utils.makeTime(source_time) + ")");
				} else {
					result.add(Utils.stationsById.get(source_id) + " ("
							+ Utils.makeTime(last_dest_time) + " - "
							+ Utils.makeTime(source_time) + ")");
				}
				last_dest_id = dest_id;
				last_dest_time = dest_time;
			}
			result.add(Utils.stationsById.get(last_dest_id) + " ("
					+ Utils.makeTime(last_dest_time) + ")");
			break;
		}
		return result;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_route_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mTrainId >= 0) {
			int[] train_path = HaRailAPI.getWholeTrainPath(mTrainId);
			((ListView) rootView.findViewById(R.id.route_detail))
					.setAdapter(new ArrayAdapter<String>(getActivity(),
							android.R.layout.simple_list_item_activated_1,
							android.R.id.text1, decodeData(train_path)));
		}

		return rootView;
	}
}
