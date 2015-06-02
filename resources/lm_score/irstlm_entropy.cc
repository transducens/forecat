/*
 * Copyright (C) 2011 Universitat d'Alacant / Universidad de Alicante
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

#include <iostream>
using namespace std;

#include "irstlm_scorer.h"

int main(int argc, char **argv) {

  // Is this really necessary?
  if(setlocale(LC_CTYPE, "") == NULL) {
    cerr << L"Warning: unsupported locale, fallback to \"C\"" << endl;
    setlocale(LC_ALL, "C");
  }

  if (argc<1) {
    cerr<<"Error: Wrong number of parameters"<<endl;
    cerr<<"Usage: "<<argv[0]<<" lm_file"<<endl;
    exit(EXIT_FAILURE);
  }

  bool val = load(argv[1], 1.0);
  //~ read1GramFile(argv[2]);

  if(!val) {
    cerr<<"There was a problem when loadling the language model from file '"<<argv[1]<<"'"<<endl;
    exit(EXIT_FAILURE);
  }
	string word;
	string line;
	string completingLine;
	int lineNumber = 0;
	vector<pair<string, double> >::iterator scoreIt;
	line = "NOT EMPTY";
  while (line.length() > 0)
  {
		
    getline(cin, line);
    if (line.length() > 0)
    {
		double pp;
		score(line, pp);
		cout <<pp <<endl;
	}
	lineNumber++;
    
    cin.clear();
  }

  return EXIT_SUCCESS;
}
