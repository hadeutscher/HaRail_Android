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

#ifndef __COMMON_H__
#define __COMMON_H__

#define STATIC_CLASS(x) private: x() = delete;
#define UNCOPYABLE_CLASS(x) private: x(const x&) = delete; x& operator=(const x&) = delete;

using namespace std;

namespace HaRail {
#ifdef _DEBUG
#ifdef _WIN32
	static const char *DATA_ROOT = "C:/irw_gtfs/";
#else
	static const char *DATA_ROOT = "~/irw_gtfs/";
#endif
#else // _DEBUG
#ifdef ANDROID
	static const char *DATA_ROOT = "/sdcard/irw_gtfs/";
#else // ANDROID
	static const char *DATA_ROOT = "./irw_gtfs/";
#endif // ANDROID
#endif // _DEBUG

	static const unsigned int SWITCH_COST = 60; // This minimizes train switches
	static const unsigned int MOVEMENT_COST = 1; // This minimizes train movements, to prevent e.g. going a->b->c->d->c->b instead of a->b->c->b, if they have the same dest time

	class Station;
	class Train;
	class Node;
	class Edge;
	class IDataSource;
	class TestDataSource;
	class GTFSDataSource;
	class HaException;
	class ArgumentParser;
	class Utils;
	class Graph;
	class StringTokenizer;

	// HashedPair is not included here because VC is stupid
}
#endif //__COMMON_H__
