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

#include "Utils.h"

namespace HaRail {
	int Utils::parseTime(const string& time)
	{
		int result = 0;
		switch (time.length())
		{
		case 8:
		{
				  if (time.substr(5, 1) != ":") {
					  throw HaException("bad time format", HaException::CONVERSION_ERROR);
				  }
				  string sec_str(time.substr(6, 2));
				  int seconds;
				  try {
					  seconds = str2int(sec_str);
				  }
				  catch (boost::bad_lexical_cast) {
					  throw HaException("bad time format", HaException::CONVERSION_ERROR);
				  }
				  result += seconds;
		}
			// FALLTHROUGH
		case 5:
		{
				  if (time.substr(2, 1) != ":") {
					  throw HaException("bad time format", HaException::CONVERSION_ERROR);
				  }
				  string hour_str(time.substr(0, 2));
				  string min_str(time.substr(3, 2));
				  int hours, minutes;
				  try {
					  hours = str2int(hour_str);
					  minutes = str2int(min_str);
				  }
				  catch (boost::bad_lexical_cast) {
					  throw HaException("bad time format", HaException::CONVERSION_ERROR);
				  }
				  result += hours * 3600 + minutes * 60;
				  break;
		}

		default:
			throw HaException("bad time format", HaException::CONVERSION_ERROR);
		}

		return result;
	}

	string Utils::padWithZeroes(const string& data, unsigned int target_len)
	{
		string result(data);
		while (result.length() < target_len) {
			result = "0" + result;
		}
		return result;
	}

	string Utils::makeTime(int time, bool short_form)
	{
		string hours = padWithZeroes(int2str(time / 3600), 2);
		time %= 3600;
		string mins = padWithZeroes(int2str(time / 60), 2);
		if (short_form) {
			return hours + ":" + mins;
		}
		else {
			string secs = padWithZeroes(int2str(time % 60), 2);
			return hours + ":" + mins + ":" + secs;
		}
	}

	void Utils::readFile(const string& path, char **out_buf)
	{
		ifstream ifs(path, ios::in | ios::binary | ios::ate);
		if (!ifs.good()) {
			throw HaException("Could not read file", HaException::FILE_NOT_FOUND_ERROR);
		}
		unsigned int size = (unsigned int)ifs.tellg();
		if (size == UINT_MAX) {
			exit(0);
		}
		char *buf = new char[size + 1];
		if (!buf) {
			throw HaException("Not enough memory", HaException::MEMORY_ERROR);
		}
		ifs.seekg(0, ios_base::beg);
		ifs.read(buf, size);
		buf[size] = 0;
		*out_buf = buf;
	}

	void Utils::readFilePart(const string& path, char **out_buf, unsigned int start, unsigned int length)
	{
		ifstream ifs(path, ios::in | ios::binary);
		if (!ifs.good()) {
			throw HaException("Could not read file", HaException::FILE_NOT_FOUND_ERROR);
		}
		if (length == UINT_MAX) {
			exit(0);
		}
		char *buf = new char[length + 1];
		if (!buf) {
			throw HaException("Not enough memory", HaException::MEMORY_ERROR);
		}
		ifs.seekg(start, ios_base::beg);
		ifs.read(buf, length);
		buf[length] = 0;
		*out_buf = buf;
	}

	string Utils::getCurrentDate()
	{
		boost::posix_time::ptime pt = boost::posix_time::second_clock::local_time();
		boost::gregorian::date d = pt.date();
		string date = string(padWithZeroes(int2str(d.day().as_number()), 2) + padWithZeroes(int2str(d.month().as_number()), 2) + padWithZeroes(int2str(d.year() % 100), 2));
		return date;
	}
}
