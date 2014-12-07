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

#ifndef __UTILS_H__
#define __UTILS_H__

#include "common.h"
#include "HaException.h"
#include <fstream>
#include <boost/lexical_cast.hpp>
#include <boost/date_time.hpp>

namespace HaRail {
	class Utils {
	public:
		static int str2int(const string& str) { return boost::lexical_cast<int, string>(str); }
		static string int2str(int i) { return boost::lexical_cast<string, int>(i); }
		static int parseTime(const string& time);
		static string padWithZeroes(const string& data, unsigned int target_len);
		static string makeTime(int time, bool short_form);
		static void readFile(const string& path, char **out_buf);
		static void readFilePart(const string& path, char **out_buf, unsigned int start, unsigned int length);
		static string getCurrentDate();
		template<typename T>
		static void writeObject(ofstream& ofs, T data) {
			ofs.write((const char *)&data, sizeof(T));
		}
		template<typename T>
		static T readObject(ifstream& ifs) {
			T result;
			ifs.read((char *)&result, sizeof(T));
			return result;
		}

		STATIC_CLASS(Utils);
		UNCOPYABLE_CLASS(Utils);
	};
}
#endif //__UTILS_H__
