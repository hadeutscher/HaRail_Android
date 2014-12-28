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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class DisplayRouteActivity extends Activity {

	public static final String EXTRA_DATA = "com.haha01haha01.harail.EXTRA_DATA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_route);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		((TextView)findViewById(R.id.resultLabel)).setText(intent.getStringExtra(EXTRA_DATA));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
