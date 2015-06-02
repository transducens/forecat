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


#ifndef IRSTLM_SCORER_H
#define IRSTLM_SCORER_H

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

long double fillTo (vector<string> s_unigrams, int number)
{
	long double result = 0;
	if (number < 1)
	{
		s_unigrams.push_back("</s>");
				
		int lmId = 0; 
		ngram m_lmtb_ng(m_lmtb.getDict());
		
		for (unsigned int i = 0; i < s_unigrams.size(); i++) {
			lmId = m_lmtb.getDict()->encode(s_unigrams.at(i).c_str()); 
			m_lmtb_ng.pushc(lmId);
			
			//~ cout <<s_unigrams.at(i) <<" ";
		}		
		result = m_lmtb.clprob(m_lmtb_ng);
		result = pow(10, result);
		//~ cout <<"\t" <<aux <<endl;
		return result;
	}
	
	
	for (std::vector<string>::iterator word = dictionary.begin() ; word != dictionary.end(); ++word)
	{
		s_unigrams.push_back(*word);
		
		//~ cout <<*word <<" " <<m_lmtb.getDict()->encode(word->c_str()) <<endl;
		
		result += fillTo(s_unigrams, number-1);
		s_unigrams.pop_back();
	}
	return result;
}

double scoreAndFuture(const string &frame, double &pp) {
  string buf;
  vector<string> s_unigrams;
  deque<string> buffer;

  stringstream ss(frame); 
  int lmId = 0; 
  //~ int toDelete = 0;                  
  double prob = 0, sprob = 0;
  unsigned int i, j;
  double aux;

  while (ss >> buf) {
    s_unigrams.push_back(buf);
  }

	for (i = 0; i < s_unigrams.size(); i++) {
		ngram m_lmtb_ng(m_lmtb.getDict());
		for (j = max((int)(i - m_nGramOrder), 0); j <= i; j++) {
			lmId = m_lmtb.getDict()->encode(s_unigrams.at(j).c_str()); 
			m_lmtb_ng.pushc(lmId);
		}
		prob = m_lmtb.clprob(m_lmtb_ng);

		sprob += prob;
	}
	
	/*
	//For the last (order of ngram)-1 tokens in the sentence
	//~ for (i = max(((int)(s_unigrams.size()-m_nGramOrder+1)), 0); i < s_unigrams.size(); i++) {
		//~ ngram m_lmtb_ng(m_lmtb.getDict());
		//~ vector<string> s_partial_unigrams;
		//~ for (j = max((int)(i), 0); j < s_unigrams.size(); j++) {
			//~ lmId = m_lmtb.getDict()->encode(s_unigrams.at(j).c_str()); 
			//~ m_lmtb_ng.pushc(lmId);
			//~ s_partial_unigrams.push_back(s_unigrams.at(j));
			//~ cout <<s_unigrams.at(j) <<" ";
		//~ }
		//~ 
		//~ aux = fillTo(s_partial_unigrams,toComplete);
		//~ sprob += aux * multiplier;
		//~ multiplier *= (1-aux*multiplier);
		//~ cout <<"\t" << aux <<" " <<multiplier <<" " << sprob <<endl;
		//~ toComplete++;
	//~ } */
	
	vector<string> s_partial_unigrams;
	for (i = max(((int)(s_unigrams.size()-m_nGramOrder+1)), 0); i < s_unigrams.size(); i++) {
		s_partial_unigrams.push_back(s_unigrams.at(i));
		//~ cout <<s_unigrams.at(i) <<" " ;
	}
	aux = fillTo(s_partial_unigrams, m_nGramOrder-2);
	sprob += log10(aux);
	//~ cout <<aux <<" " <<log10(aux) <<" "<< sprob <<endl;
	

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

#endif
