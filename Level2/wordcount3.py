#! /usr/bin/python
# -*-coding:utf-8 -*

from mrjob.job import MRJob
from mrjob.step import MRStep

class Count(MRJob):

    def mapper_count(self, _, line):
        for word in line.split():
            yield(word.lower(), 1)
    
    def reducer_count(self, word, counts):
        yield None, (sum(counts), word)
    
    def reducer_max(self, _, reduced_pairs):
        # Find max
        yield max(reduced_pairs)
        
    def steps(self):
        return [
            MRStep(
                mapper=self.mapper_count,
                reducer=self.reducer_count
            ),
            MRStep(reducer=self.reducer_max),
        ]

if __name__ == '__main__':
    Count.run()
