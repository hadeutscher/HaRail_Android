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

#include "lib_main.h"

namespace HaRail {
	string lib_main(int date, int start_station_id, int start_time, int dest_station_id)
	{
		try {
			GTFSDataSource gds(DATA_ROOT, Utils::padWithZeroes(Utils::int2str(date), 6));
			gds.initStations();
			gds.initTrains();
			vector<Train *> shortest_route;
			vector<Train *> best_route;
			Graph::getBestRoutes(&gds, gds.getStationById(start_station_id), start_time, gds.getStationById(dest_station_id), shortest_route, best_route);
			stringstream ss;
			Graph::printBestRoutes(shortest_route, best_route, ss);
			return ss.str();
		}
		catch (HaException e) {
			return e.what();
		}
		catch (...) {
			return "Unknown Error";
		}
	}
}
