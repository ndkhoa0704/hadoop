#! /usr/bin/python
# -*-coding:utf-8 -*

from mrjob.job import MRJob
class Count(MRJob):

    def mapper(self, _, line):
        # map a word with number 1
        for word in line.split():
            yield(word, 1)
    
    def reducer(self, word, counts):
        # sum value of pairs with similar keys
        yield(word, sum(counts))
        
if __name__ == '__main__':
    Count.run()
