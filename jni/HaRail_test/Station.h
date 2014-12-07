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

#ifndef __STATION_H__
#define __STATION_H__

#include "common.h"
#include <string>
#include <vector>

namespace HaRail {
	class Station {
	public:
		// Class Methods
		Station(int station_id, string station_name)
			: station_id(station_id),
			station_name(station_name)
		{}
		virtual ~Station() {}

		// Property Accessors
		int getStationId() const { return station_id; }
		const string& getStationName() const { return station_name; }

	protected:
		// Fields
		int station_id;
		string station_name;

		UNCOPYABLE_CLASS(Station);
	};
}
#endif //__STATION_H__
