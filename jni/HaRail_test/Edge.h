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

#ifndef __EDGE_H__
#define __EDGE_H__

#include "common.h"

namespace HaRail {
	class Edge {
	public:
		// Class Methods
		Edge(Train *train, Node *source, Node *dest, int cost)
			: train(train),
			source(source),
			dest(dest),
			cost(cost) {}
		virtual ~Edge() {};

		// Property Accessors
		Train *getTrain() const { return train; }
		Node *getSource() const { return source; }
		Node *getDest() const { return dest; }
		int getCost() const { return cost; }

	protected:
		// Fields
		Train *train;
		Node *source;
		Node *dest;
		int cost;

		UNCOPYABLE_CLASS(Edge);
	};
}
#endif //__EDGE_H__
