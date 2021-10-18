# Instructions
# 'ParserAndIndex' -> 'Querier' -> 'trec_eval'

1. cd ParseAndIndex/   (enter indexer directory)
2. mvn package  (build JAR with maven)
3. java -jar target/Project1-1.0.jar ../cran/cran.all.1400  (run jar of indexer)
4. cd .. (return to main directory)

4. cd Querier (enter Querier directory)
5. mvn package  (build JAR with maven)
6. java -jar target/Project1-1.0.jar ../cran/cran.qry (run jar of Querier to generate results file -> this feeds into trec_eval)
7. cd .. (return to main directory)

8. cd trec_eval-master  (enter trec_eval directory)
9. ./trec_eval  ../cran/qrels ../cran/results.txt (run the trec_eval program to compare the corrected cran relevance scores with generated relevance scores)
