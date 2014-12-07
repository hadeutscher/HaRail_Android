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

#ifndef __LIB_MAIN_H__
#define __LIB_MAIN_H__

#include "common.h"
#include "Utils.h"
#include "HaException.h"
#include "IDataSource.h"
#include "GTFSDataSource.h"
#include "Station.h"
#include "Train.h"
#include "Node.h"
#include "Edge.h"
#include "Graph.h"
#include <sstream>

namespace HaRail {
	string lib_main(int date, int start_station_id, int start_time, int dest_station_id);
}

#endif //__LIB_MAIN_H__
