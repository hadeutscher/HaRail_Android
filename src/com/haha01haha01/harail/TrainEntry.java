/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

public class TrainEntry {
	public int id;
	public String text;

	public TrainEntry(int id, String text) {
		this.id = id;
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
