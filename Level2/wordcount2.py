#! /usr/bin/python
# -*-coding:utf-8 -*

from mrjob.job import MRJob
class Count(MRJob):

    def mapper(self, _, line):
        # Similar to version 1
        for word in line.split():
            # Lower case words to ignore case sensitive
            yield(word.lower(), 1)
    
    def reducer(self, word, counts):
        yield(word, sum(counts))
        
if __name__ == '__main__':
    Count.run()
