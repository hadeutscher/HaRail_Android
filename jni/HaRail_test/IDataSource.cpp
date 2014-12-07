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

#include "IDataSource.h"

namespace HaRail {
	IDataSource::~IDataSource()
	{
		for (Station *station : stations) {
			delete station;
		}

		for (Train *train : trains) {
			delete train;
		}
	}

	Station *IDataSource::createStation(int station_id, const string& station_name)
	{
		Station *station = new Station(station_id, station_name);
		stations.push_back(station);
		stations_by_id.emplace(station_id, station);
		stations_by_name.emplace(station_name, station);
		return station;
	}

	Train *IDataSource::createTrain(int train_id, Station *source, Station *dest, int source_time, int dest_time)
	{
		Train *train = new Train(train_id, source, dest, source_time, dest_time);
		trains.push_back(train);
		return train;
	}

	void IDataSource::listStations()
	{
		for (Station *station : stations) {
			cout << station->getStationId() << " : " << station->getStationName().c_str() << endl;
		}
	}
}
