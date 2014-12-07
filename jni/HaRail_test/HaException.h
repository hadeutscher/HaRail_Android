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

#ifndef __HAEXCEPT_H__
#define __HAEXCEPT_H__

#include "common.h"
#include <stdexcept>
#include <string>

namespace HaRail {
	class HaException : public runtime_error {
	public:
		enum type {
			CONVERSION_ERROR,
			FILE_NOT_FOUND_ERROR,
			MEMORY_ERROR,
			UNIMPLEMENTED_ERROR,
			CRITICAL_ERROR,
			INVALID_ROUTE_ERROR,
			DATABASE_FORMAT_ERROR
		};
		HaException(const string& message, type type)
			: runtime_error(message), type(type) {}

		type getType() const { return type; }

	protected:
		type type;
	};
}
#endif //__HAEXCEPT_H__
