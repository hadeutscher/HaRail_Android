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

#include "GTFSDataSource.h"

namespace HaRail {
	void GTFSDataSource::initStations()
	{
		char *buf;
		Utils::readFile(root_path + "stops.txt", &buf);
		char *first_line = strstr(buf, "\r\n");
		if (!first_line) {
			throw HaException("bad database format", HaException::DATABASE_FORMAT_ERROR);
		}
		first_line += 2;
		StringTokenizer tokenizer(first_line, "\r\n");
		vector<string> line_split;

		for (string line : tokenizer) {
			StringTokenizer line_tokenizer(line.c_str(), ",");
			StringTokenizer::iterator i = line_tokenizer.begin();
			int station_id = Utils::str2int(*i++);
			string station_name = *i++;
			createStation(station_id, station_name);
		}

		delete[] buf;
	}

	void GTFSDataSource::initTrains()
	{
		char *buf;
		pair<int, int> index = getDateIndex();
		Utils::readFilePart(root_path + "stop_times.txt", &buf, index.first, index.second);
		loadTrainsForDate(buf);

		delete[] buf;
	}

	void GTFSDataSource::loadTrainsForDate(char *buf)
	{
		StringTokenizer tokenizer(buf, "\r\n");

		int curr_train_id = -1;
		int curr_seq = -1;
		Station *last_station = nullptr;
		int last_time = -1;

		for (string line : tokenizer) {
			// Parse the line
			StringTokenizer line_tokenizer(line.c_str(), ",");
			StringTokenizer::iterator i = line_tokenizer.begin();
			string date_id = *i++;
			int dw_time1 = Utils::parseTime(*i++);
			int dw_time2 = Utils::parseTime(*i++);
			Station *station = getStationById(Utils::str2int(*i++));
			int seq = Utils::str2int(*i++);
			StringTokenizer date_id_tokenizer(date_id.c_str(), "_");
			i = date_id_tokenizer.begin();
			string train_date = *i++;
			int train_id = Utils::str2int(*i++);

			if (train_date != date) {
				throw HaException("bad database format", HaException::DATABASE_FORMAT_ERROR);
			}
			if (curr_train_id != train_id) {
				// new train
				curr_train_id = train_id;
				curr_seq = seq;
				if (curr_seq != 1) {
					throw HaException("bad database format", HaException::DATABASE_FORMAT_ERROR);
				}
			}
			else {
				if (seq != ++curr_seq) {
					throw HaException("bad database format", HaException::DATABASE_FORMAT_ERROR);
				}
				createTrain(train_id, last_station, station, last_time, dw_time1);
			}
			last_station = station;
			last_time = dw_time2;
		}
	}

	pair<int, int> GTFSDataSource::getDateIndex() const
	{
		ifstream ifs(root_path + "HaRail.idx", ios::in | ios::binary);

		while (!ifs.good() || Utils::readObject<unsigned int>(ifs) != INDEXER_VERSION) {
			cout << "New database found, indexing..." << endl;
			if (ifs.is_open()) {
				ifs.close();
			}
			indexDatabase();
			ifs.open(root_path + "HaRail.idx", ios::in | ios::binary);
		}
		
		unsigned int date_uint;
		try {
			date_uint = Utils::str2int(date);
		}
		catch (boost::bad_lexical_cast) {
			throw HaException("Invalid date", HaException::CONVERSION_ERROR);
		}

		while (!ifs.eof()) {
			unsigned int offs = Utils::readObject<unsigned int>(ifs);
			unsigned int date_field = Utils::readObject<unsigned int>(ifs);
			if (date_field == date_uint) {
				unsigned int end_offs = Utils::readObject<unsigned int>(ifs);
				return pair<int, int>(offs, end_offs - offs);
			}
		}
		throw HaException("Invalid date or database too old", HaException::DATABASE_FORMAT_ERROR);
	}

	void GTFSDataSource::indexDatabase() const
	{
		char *buf;
		Utils::readFile(root_path + "stop_times.txt", &buf);
		char *first_line = strstr(buf, "\r\n");
		if (!first_line) {
			throw HaException("bad database format", HaException::DATABASE_FORMAT_ERROR);
		}
		first_line += 2;

		ofstream ofs(root_path + "HaRail.idx", ios::out | ios::binary | ios::trunc);
		Utils::writeObject<unsigned int>(ofs, INDEXER_VERSION);
		string last_date("XXXXXX");
		StringTokenizer tokenizer(first_line, "\r\n");
		for (StringTokenizer::iterator i = tokenizer.begin(), end = tokenizer.end(); i != end; ++i) {
			string line = *i;
			if (line.length() < 8 || line.c_str()[6] != '_') {
				throw HaException("bad database format", HaException::DATABASE_FORMAT_ERROR);
			}
			if (memcmp(last_date.c_str(), line.c_str(), 6)) {
				last_date = line.substr(0, 6);
				Utils::writeObject<unsigned int>(ofs, i.getPosition() - buf);
				Utils::writeObject<unsigned int>(ofs, Utils::str2int(last_date));
			}
		}
		Utils::writeObject<const char *>(ofs, tokenizer.end().getPosition());
		Utils::writeObject<unsigned int>(ofs, UINT_MAX);
		delete[] buf;
	}
}
