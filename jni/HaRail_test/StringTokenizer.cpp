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

#include "StringTokenizer.h"

namespace HaRail {
	const char *StringTokenizer::getNextToken(const char *curr) const
	{
		const char *next = strstr(curr, token);
		return next ? next : buf_end;
	}

	void StringTokenizer::advanceIterator(const char **curr, const char **next_tok) const
	{
		if (*next_tok != buf_end) {
			*curr = *next_tok + token_len;
			*next_tok = getNextToken(*curr);
		}
		else {
			*curr = buf_end;
		}
	}

	StringTokenizer::iterator& StringTokenizer::iterator::operator++()
	{
		parent->advanceIterator(&pos, &next_tok);
		return *this;
	}

	StringTokenizer::iterator StringTokenizer::iterator::operator++(int unused)
	{
		StringTokenizer::iterator result = *this;
		parent->advanceIterator(&pos, &next_tok);
		return result;
	}
}
