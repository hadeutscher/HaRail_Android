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

#ifndef __TRAIN_H__
#define __TRAIN_H__

#include "common.h"

namespace HaRail {
	class Train {
	public:
		// Class Methods
		Train(int train_id, Station *source, Station *dest, int source_time, int dest_time)
			: train_id(train_id),
			source(source),
			dest(dest),
			source_time(source_time),
			dest_time(dest_time) {}
		virtual ~Train() {};

		// Methods
		int getCost() const { return dest_time - source_time; }

		// Property Accessors
		int getTrainId() const { return train_id; }
		Station *getSource() const { return source; }
		Station *getDest() const { return dest; }
		int getSourceTime() const { return source_time; }
		int getDestTime() const { return dest_time; }

	protected:
		// Fields
		int train_id;
		Station *source;
		Station *dest;
		int source_time;
		int dest_time;

		UNCOPYABLE_CLASS(Train);
	};
}
#endif //__TRAIN_H__
