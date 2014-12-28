/* HaRail - Public transport fastest-route finder for Israel Railways
 * Copyright(C) 2014  haha01haha01

 * This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

 * This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package com.haha01haha01.harail;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
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

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
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
			int last_dest_time = -1;
			for (int j = 0; j < train_count; j++) {
				int source_id = path[i++];
				int source_time = path[i++];
				i++; //int dest_id = path[i++]; // Unused variable
				int dest_time = path[i++];
				if (last_dest_time == -1 || last_dest_time == source_time) {
					result.add(Utils.stationsById.get(source_id) + " ("
							+ Utils.makeTime(source_time) + ")");
				} else {
					result.add(Utils.stationsById.get(source_id) + " ("
							+ Utils.makeTime(last_dest_time) + " - "
							+ Utils.makeTime(source_time) + ")");
				}
				last_dest_time = dest_time;
			}
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
