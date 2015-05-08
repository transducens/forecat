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

#include <stdlib.h>
#include <math.h>
#include <sstream>
#include <iostream>
#include <getopt.h>
#include <clocale>
#include <vector>
#include <deque>

#include "lmtable.h"
#include "lmmacro.h"

//~ class lmtable;  // irst lm table
//~ class lmmacro;  // irst lm for macro tags

using namespace std;

irstlm::lmtable m_lmtb;

int            m_unknownId;
int            m_lmtb_size;          // max ngram stored in the table
int            m_lmtb_dub;           // dictionary upperbound

float          m_weight; // scoring weight.
string         m_filePath; // for debugging purposes.
size_t         m_nGramOrder; // max n-gram length contained in this LM.

vector<string> dictionary;

const int UPTO = 1; //Maximum number of branches

void read1GramFile(const string &filePath)
{
	ifstream file;
	file.open(filePath.c_str(), ios::in);
	string line;
	
	while ( getline (file,line) )
	{
		dictionary.push_back(line);
	}
	
	cerr << "Vocab size " << dictionary.size() <<endl;
}

bool load(const string &filePath, float weight) {
  m_weight  = weight;

  m_filePath = filePath;

  // Open the input file (possibly gzipped)
  std::filebuf fb;
  fb.open(filePath.c_str(), std::ios::in);
  std::istream inp((std::streambuf*)&fb);

  // case (standard) LMfile only: create an object of lmtable

  //~ m_lmtb  = (irstlm::lmtable *)new irstlm::lmtable;
  if (m_filePath.compare(m_filePath.size()-3,3,".mm")==0) {
    m_lmtb.load(inp, m_filePath.c_str(), NULL, 1);
  } else {
    m_lmtb.load(inp, m_filePath.c_str(), NULL, 0);
  }


  m_lmtb_size = m_lmtb.maxlevel();       
  m_nGramOrder = m_lmtb.maxlevel();       

  // LM can be ok, just outputs warnings

  m_unknownId = m_lmtb.getDict()->oovcode(); // at the level of micro tags

  cerr<<"IRST: m_unknownId="<<m_unknownId<<endl;

  //install caches
  m_lmtb.init_probcache();
  m_lmtb.init_statecache();
  m_lmtb.init_lmtcaches(m_lmtb.maxlevel() > 2 ? (m_lmtb.maxlevel() - 1) : 2);
 
  if (m_lmtb_dub >0) m_lmtb.setlogOOVpenalty(m_lmtb_dub);
    
  return true;
}

double score(const string &frame, double &pp) {
  string buf;
  vector<string> s_unigrams;
  deque<string> buffer;

  stringstream ss(frame); 
  int lmId = 0; 
  //~ int toDelete = 0;                  
  float prob = 0, sprob = 0;

  while (ss >> buf) {
    s_unigrams.push_back(buf);
  }

	//~ toDelete = s_unigrams.size() - (m_nGramOrder + 2);
	//~ if (toDelete > 0)
	//~ {
		//~ s_unigrams.erase(s_unigrams.begin() + 1, s_unigrams.begin() + toDelete + 1); 
	//~ }

	for (unsigned int i = 0; i < s_unigrams.size(); i++) {
		ngram m_lmtb_ng(m_lmtb.getDict());
		for (unsigned int j = max((int)(i - m_nGramOrder), 0); j <= i; j++) {
			lmId = m_lmtb.getDict()->encode(s_unigrams.at(j).c_str()); 
			m_lmtb_ng.pushc(lmId);
		}
		prob = m_lmtb.clprob(m_lmtb_ng);

		sprob += prob;
	}

  //Perplexity
  pp = exp((-sprob * log(10.0))/max((int)s_unigrams.size()-1, 1));  //Do not take into account <s>, but </s>

  return sprob;
}

vector<vector<pair<string, double> > >* getAllLengthSuggestions(string line, int maxLength = 5)
{
	double pp;
	vector<pair<string, double> > best, newBest;
	vector<vector<pair<string, double> > >* toRet = new vector<vector<pair<string, double> > >();
	vector<string> partString;
	vector<pair<string, double> >::iterator scoreIt;
	int suggestionLength = 0, numberOfSpaces;
	string prefix_of_suffix;
	unsigned int i;
	
	unsigned int lastSpace = line.length();
	while (lastSpace > 0 && line[lastSpace] != ' ') {lastSpace--;}
	
	if (lastSpace <= 0)
	{
		best.push_back(std::make_pair("", 99999999));
	}
	else
	{
		best.push_back(std::make_pair(line.substr(0, lastSpace) + " ", 99999999));
	}
	prefix_of_suffix = line.substr(lastSpace>0?lastSpace+1:0);	//The last partially typed word 
		//~ 
	//~ cerr <<lastSpace <<endl;
	//~ cerr <<line.length() <<endl;
	//~ cerr <<	line.substr(0, lastSpace) <<endl;
	//~ cerr <<	best.begin()->first <<endl;
	//~ cerr <<prefix_of_suffix <<endl;
	//~ cerr <<(prefix_of_suffix != "") <<endl;
	//~ cerr <<"-" <<endl;
		
	while (suggestionLength < maxLength)
	{
		suggestionLength ++;
		newBest.clear();
		partString.clear();
		
		//Because we are comparing suffixes, model wont affect further than the order (plus the extra word we add)
		for (std::vector<pair<string, double> >::iterator prefix = best.begin() ; prefix != best.end(); ++prefix)
		{
			numberOfSpaces = 0;
			for (i = 0; i < prefix->first.length(); i++)
			{
				if (prefix->first[i] == ' ')
				{
					numberOfSpaces++;
				}
			}
			
			if (numberOfSpaces > (int)(m_nGramOrder-1))
			{	
				for (i = 0; i < prefix->first.length() && numberOfSpaces > (int)(m_nGramOrder-1); i++)
				{
					if (prefix->first[i] == ' ')
					{
						numberOfSpaces--;
					}
				}

				//~ cerr <<"- " <<prefix->first <<"| " <<endl;
				//~ cerr <<"+ " <<prefix->first.substr(i) <<"|" <<endl;
				partString.push_back(prefix->first.substr(i));
			}	
			else
			{
				partString.push_back(prefix->first);
			}
		}
		
		for (std::vector<string>::iterator word = dictionary.begin() ; word != dictionary.end(); ++word)
		{
			for (std::vector<pair<string, double> >::iterator prefix = best.begin() ; prefix != best.end(); ++prefix)
			{
				if (prefix_of_suffix != "")
				{
					if (word->find(prefix_of_suffix) != 0) // If the word does not start with our prefix, ignore it
					{
						continue;
					}
				}

				//~ cerr <<prefix->first <<" : " <<prefix - best.begin() <<	" -> " <<partString[prefix - best.begin()] <<endl;
				score("<s> " + partString[prefix - best.begin()] + *word +  " </s>", pp); //prefix ends in space
				//~ score("<s> " + prefix->first + *word +  " </s>", pp); //prefix ends in space
				//Empty, just insert it
				if (newBest.size() == 0)
				{
					newBest.push_back(std::make_pair(prefix->first + *word + " ", pp));
				}
				//Not empty, look for the position on the vector
				scoreIt = newBest.begin();
				while (scoreIt != newBest.end() && scoreIt->second < pp) scoreIt++;
				//If we arent inserting it at the end of the vector, or we did not fill all the possible options
				if (scoreIt != newBest.end() || newBest.size() < UPTO)
				{
					newBest.insert(scoreIt, std::make_pair(prefix->first + *word + " ", pp));
					//If its bigger than our target, resize
					if (newBest.size() >= UPTO)
					{
						newBest.resize(UPTO);
					}
				}
			}
		}
		prefix_of_suffix = "";
		best = newBest;
		
		for (std::vector<pair<string, double> >::iterator prefix = newBest.begin() ; prefix != newBest.end(); ++prefix)
		{
			prefix->first = prefix->first.substr(lastSpace==0?lastSpace:lastSpace+1);
		}
		
		toRet->push_back(newBest);
	}

	return toRet;
}

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
    cerr<<"There was a problem when loading the language model from file '"<<argv[1]<<"'"<<endl;
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
		
		cout <<score(line, pp) <<endl;
	}
	lineNumber++;
    
    cin.clear();
  }

  return EXIT_SUCCESS;
}
