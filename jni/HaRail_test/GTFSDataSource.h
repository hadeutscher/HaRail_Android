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

#ifndef __GTFS_H__
#define __GTFS_H__

#include "common.h"
#include "Utils.h"
#include "IDataSource.h"
#include "Station.h"
#include "Train.h"
#include "StringTokenizer.h"
#include "HaException.h"

namespace HaRail {
	class GTFSDataSource : public IDataSource {
	public:
		// Class Methods
		GTFSDataSource(const string& root, const string& date) : root_path(root), date(date) {}
		virtual ~GTFSDataSource() {}
		virtual void initStations();
		virtual void initTrains();

		// Constants
		static const unsigned int INDEXER_VERSION = 1;

	protected:
		// Private Methods
		void loadTrainsForDate(char *start);
		pair<int, int> getDateIndex() const;
		void indexDatabase() const;

		// Fields
		string root_path;
		string date;

		UNCOPYABLE_CLASS(GTFSDataSource);
	};
}
#endif //__GTFS_H__
