/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class allRoutesListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private static final String STATE_ENCODED_DATA = "encoded_data";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * The encoded data passed from routeListActivity
	 */
	private int[] mEncodedData;

	/**
	 * The list of entries
	 */
	List<RouteEntry> mItems;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(int id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(int id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public allRoutesListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ENCODED_DATA)) {
			mEncodedData = savedInstanceState.getIntArray(STATE_ENCODED_DATA);
		}
		
		decodeData();

		setListAdapter(new ArrayAdapter<RouteEntry>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, mItems));
	}

	private String makeRoute(int source_time, int dest_time, int train_count) {
		return "(" + Utils.makeTime(source_time) + ") -> "
				+ "(" + Utils.makeTime(dest_time) + ")"
				+ ((train_count > 1) ? (" (+" + (train_count - 1) + ")") : "");
	}

	private int appendRouteToList(int i) {
		int source_time = mEncodedData[i++];
		int dest_time = mEncodedData[i++];
		int train_count = mEncodedData[i++];
		mItems.add(new RouteEntry(source_time, makeRoute(source_time, dest_time, train_count)));
		return i;
	}

	private void decodeData() {
		mItems = new ArrayList<RouteEntry>();

		int i = 0;

		switch (mEncodedData[i++]) {
		case 0:
			// Error
			mItems.add(new RouteEntry(-1, HaRailAPI.getLastError()));
			break;
		case 1:
			while (i < (mEncodedData.length - 1)) {
				i = appendRouteToList(i);
			}
			break;
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
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
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;

		mEncodedData = activity.getIntent().getIntArrayExtra(
				routeListActivity.EXTRA_DATA);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		int item_id = mItems.get(position).start;
		if (item_id >= 0) {
			mCallbacks.onItemSelected(item_id);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putIntArray(STATE_ENCODED_DATA, mEncodedData);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}
