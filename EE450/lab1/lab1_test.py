#!/usr/bin/python3

import os
import sys
import random
import unittest
import subprocess

import numpy as np
import networkx as nx
import matplotlib.pyplot as plt


check_output = lambda x: subprocess.check_output(x, universal_newlines=True)


def save(G, filename="network.txt", source_dest=None):
  """
  Save graph G to file using the specified format
  """

  # Select the source and target nodes base on their centrality
  nodes   = []
  weights = []
  for node, centrality in nx.betweenness_centrality(G).items():
    nodes.append(node)
    weights.append(1.0 - centrality)

  if source_dest is None:
    S, T = random.choices(nodes, weights, k=2)
    
    # Source and target nodes should be different
    while S == T:
      S, T = random.choices(nodes, weights, k=2)
  else:
    S, T = source_dest

  lines = list(nx.generate_edgelist(G, data=["delay", "price"]))
  lines = ["{} {} {}".format(len(lines), S, T)] + lines
  with open(filename, "w") as f:
    f.write("\n".join(lines))

  return S, T


class Lab1TestCycle(unittest.TestCase):

  def setUp(self):
    """
    Compile the program before testing
    """
    subprocess.check_call(["make", "lab1"])


  def load_result(self, filename="result.txt"):
    """
    Load result from file.
    """

    with open(filename) as f:
      datalines = f.readlines()

    # First line should contain a number
    return int(datalines[0])

  
  def test_cycle_noneg(self):
    """
    Graph with a cycle but not negative
    """
    # Create a list of graphs containing 6 nodes and cycle(s)
    GRAPHS = nx.generators.atlas.graph_atlas_g()
    GRAPHS = list(filter(lambda g: nx.is_connected(g) and g.number_of_nodes() == 6 and nx.cycle_basis(g), GRAPHS[1:]))
    
    # Select a random graph
    G = random.sample(GRAPHS, 1)[0] 

    # Generate random attributes
    edge_attr = {e: {"delay": random.randint(0, 9), "price":random.randint(0, 9)} for e in G.edges}
    nx.set_edge_attributes(G, edge_attr)

    # Write to file
    input_file = "test-negcycle-cycle.txt"
    save(G, filename=input_file)

    # Run user program
    subprocess.check_call(["./lab1.out", input_file])

    reference = 0
    result    = self.load_result()

    self.assertEqual(reference, result, msg="Reference and user result do not match on a graph with positive cycles.")



  def test_cycle_neg(self):
    """
    Graph with a cycle and negative-weight one
    """
    # Create a list of graphs containing 6 nodes and cycle(s)
    GRAPHS = nx.generators.atlas.graph_atlas_g()
    GRAPHS = list(filter(lambda g: nx.is_connected(g) and g.number_of_nodes() == 6 and nx.cycle_basis(g), GRAPHS[1:]))
    
    # Select a random graph
    G = random.sample(GRAPHS, 1)[0] 

    # Generate random attributes
    edge_attr = {e: {"delay": random.randint(0, 9), "price":random.randint(0, 9)} for e in G.edges}

    # Create a random negative cycle
    cycle = random.sample(nx.cycle_basis(G), 1)[0]
    attr  = random.sample(["delay", "price"], 1)[0]
    bias  = random.randint(-9, 0)

    if (cycle[0], cycle[1]) in edge_attr:
      edge_attr[(cycle[0], cycle[1])][attr] = bias - 10 * (len(cycle)-1)
    else:
      edge_attr[(cycle[1], cycle[0])][attr] = bias - 10 * (len(cycle)-1)

    nx.set_edge_attributes(G, edge_attr)

    # Write to file
    input_file = "test-negcycle-negcycle.txt"
    save(G, filename=input_file)

    # Run user program
    subprocess.check_call(["./lab1.out", input_file])

    reference = 1
    result    = self.load_result()

    self.assertEqual(reference, result, msg="Reference and user result do not match on a graph with negative cycles.")


  def test_nocycle(self):
    """
    Graph has no cycles
    """

    # Create a list of graphs containing 7 nodes and without cycles
    GRAPHS = nx.generators.atlas.graph_atlas_g()
    GRAPHS = list(filter(lambda g: nx.is_tree(g) and g.number_of_nodes() == 7, GRAPHS[1:]))
    
    # Select a random graph
    G = random.sample(GRAPHS, 1)[0] 

    # Generate random attributes
    edge_attr = {e: {"delay": random.randint(0, 9), "price":random.randint(0, 9)} for e in G.edges}
    nx.set_edge_attributes(G, edge_attr)

    # Write to file
    input_file = "test-negcycle-nocycle.txt"
    save(G, filename=input_file)

    # Run user program
    subprocess.check_call(["./lab1.out", input_file])

    reference = 0
    result    = self.load_result()

    self.assertEqual(reference, result, msg="Reference and user result do not match on a graph with no cycles.")


class Lab1TestCost(unittest.TestCase):

  def setUp(self):
    """
    Compile the program before testing
    """
    subprocess.check_call(["make", "lab1"])


  def load_result(self, filename="result.txt"):
    """
    Load result from file.
    """

    with open(filename) as f:
      datalines = f.readlines()

    # First number of second and third line
    return [int(l.split()[0]) for l in datalines[1:3]]

  
  def test_cycle(self):
    """
    Graph with a cycle but not negative
    """
    # Create a list of graphs containing 6 nodes and cycle(s)
    GRAPHS = nx.generators.atlas.graph_atlas_g()
    GRAPHS = list(filter(lambda g: nx.is_connected(g) and g.number_of_nodes() == 6 and nx.cycle_basis(g), GRAPHS[1:]))
    
    # Select a random graph
    G = random.sample(GRAPHS, 1)[0] 

    # Generate random attributes
    edge_attr = {e: {"delay": random.randint(0, 9), "price":random.randint(0, 9)} for e in G.edges}
    nx.set_edge_attributes(G, edge_attr)

    # Write to file
    input_file = "test-cost-cycle.txt"
    S, T = save(G, filename=input_file)

    # Run user program
    subprocess.check_call(["./lab1.out", input_file])

    reference = [nx.bellman_ford_path_length(G, S, T, "delay"), nx.bellman_ford_path_length(G, S, T, "price")]
    result    = self.load_result()

    self.assertEqual(reference, result, msg="Reference and user result do not match on a graph with positive cycles.")


  def test_nocycle_pos(self):
    """
    Graph has no cycles
    """

    # Create a list of graphs containing 7 nodes and without cycles
    GRAPHS = nx.generators.atlas.graph_atlas_g()
    GRAPHS = list(filter(lambda g: nx.is_tree(g) and g.number_of_nodes() == 7, GRAPHS[1:]))
    
    # Select a random graph
    G = random.sample(GRAPHS, 1)[0] 

    # Generate random attributes
    edge_attr = {e: {"delay": random.randint(0, 9), "price":random.randint(0, 9)} for e in G.edges}
    nx.set_edge_attributes(G, edge_attr)

    # Write to file
    input_file = "test-cost-nocycle.txt"
    S, T = save(G, filename=input_file)

    # Run user program
    subprocess.check_call(["./lab1.out", input_file])

    reference = [nx.bellman_ford_path_length(G, S, T, "delay"), nx.bellman_ford_path_length(G, S, T, "price")]
    result    = self.load_result()

    self.assertEqual(reference, result, msg="Reference and user result do not match on a graph with no cycles.")


class Lab1TestPath(unittest.TestCase):

  def setUp(self):
    """
    Compile the program before testing
    """
    subprocess.check_call(["make", "lab1"])


  def load_result(self, filename="result.txt"):
    """
    Load result from file.
    """

    with open(filename) as f:
      datalines = f.readlines()

    # All numbers except first of second and third line
    return [[int(n) for n in l.split()[1:]] for l in datalines[1:3]]

  
  def test_cycle(self):
    """
    Graph with a cycle but not negative
    """
    # Create a list of graphs containing 6 nodes and cycle(s)
    GRAPHS = nx.generators.atlas.graph_atlas_g()
    GRAPHS = list(filter(lambda g: nx.is_connected(g) and g.number_of_nodes() == 6 and nx.cycle_basis(g), GRAPHS[1:]))
    
    is_path_unique = False

    while not is_path_unique:
      # Select a random graph
      G = random.sample(GRAPHS, 1)[0] 

      # Generate random attributes
      edge_attr = {e: {"delay": random.randint(0, 9), "price":random.randint(0, 9)} for e in G.edges}
      nx.set_edge_attributes(G, edge_attr)

      # Write to file
      input_file = "test-path-cycle.txt"
      S, T = save(G, filename=input_file)

      is_path_unique = len(list(nx.all_shortest_paths(G, S, T, "delay"))) == 1 and len(list(nx.all_shortest_paths(G, S, T, "price"))) == 1

    # Run user program
    subprocess.check_call(["./lab1.out", input_file])

    reference = [nx.bellman_ford_path(G, S, T, "delay"), nx.bellman_ford_path(G, S, T, "price")]
    result    = self.load_result()

    self.assertEqual(reference, result, msg="Reference and user result do not match on a graph with positive cycles.")


  def test_nocycle_pos(self):
    """
    Graph has no cycles
    """

    # Create a list of graphs containing 7 nodes and without cycles
    GRAPHS = nx.generators.atlas.graph_atlas_g()
    GRAPHS = list(filter(lambda g: nx.is_tree(g) and g.number_of_nodes() == 7, GRAPHS[1:]))
    
    # Select a random graph
    G = random.sample(GRAPHS, 1)[0] 

    # Generate random attributes
    edge_attr = {e: {"delay": random.randint(0, 9), "price":random.randint(0, 9)} for e in G.edges}
    nx.set_edge_attributes(G, edge_attr)

    # Write to file
    input_file = "test-path-nocycle.txt"
    S, T = save(G, filename=input_file)

    # Run user program
    subprocess.check_call(["./lab1.out", input_file])

    reference = [nx.bellman_ford_path(G, S, T, "delay"), nx.bellman_ford_path(G, S, T, "price")]
    result    = self.load_result()

    self.assertEqual(reference, result, msg="Reference and user result do not match on a graph with no cycles.")


class Lab1TestSamePath(unittest.TestCase):

  def setUp(self):
    """
    Compile the program before testing
    """
    subprocess.check_call(["make", "lab1"])


  def load_result(self, filename="result.txt"):
    """
    Load result from file.
    """

    with open(filename) as f:
      datalines = f.readlines()

    # Number on the fourth line
    return int(datalines[3])

  
  def test_cycle(self):
    """
    Graph with a cycle but not negative
    """
    # Create a list of graphs containing 6 nodes and cycle(s)
    GRAPHS = nx.generators.atlas.graph_atlas_g()
    GRAPHS = list(filter(lambda g: nx.is_connected(g) and g.number_of_nodes() == 6 and nx.cycle_basis(g), GRAPHS[1:]))
    
    is_path_diff = False

    while not is_path_diff:
      # Select a random graph
      G = random.sample(GRAPHS, 1)[0]

      # Generate weights 
      delays = [2**i for i in range(len(G.edges))]
      prices = delays.copy()
      random.shuffle(delays)
      random.shuffle(prices)

      edge_attr = {e: {"delay": delays[i], "price":prices[i]} for i, e in enumerate(G.edges)}

      # Generate random attributes
      edge_attr = {e: {"delay": delays[i], "price":prices[i]} for i, e in enumerate(G.edges)}
      # edge_attr = {e: {"delay": random.randint(0, 1), "price":random.randint(0, 1)} for e in G.edges}
      nx.set_edge_attributes(G, edge_attr)

      # Select the source and target nodes base on their centrality
      nodes   = []
      weights = []
      for node, centrality in nx.betweenness_centrality(G).items():
        nodes.append(node)
        weights.append(1.0 - centrality)

      S, T = random.choices(nodes, weights, k=2)
      
      # Source and target nodes should be different
      while S == T:
        S, T = random.choices(nodes, weights, k=2)

      is_path_diff = nx.bellman_ford_path(G, S, T, "delay") != nx.bellman_ford_path(G, S, T, "price") and len(list(nx.all_shortest_paths(G, S, T, "delay"))) == 1 and len(list(nx.all_shortest_paths(G, S, T, "price"))) == 1

    # Write to file
    input_file = "test-samepath-cycle.txt"
    S, T = save(G, filename=input_file, source_dest=(S, T))

    # Run user program
    subprocess.check_call(["./lab1.out", input_file])

    reference = int(nx.bellman_ford_path(G, S, T, "delay") == nx.bellman_ford_path(G, S, T, "price"))
    result    = self.load_result()

    self.assertEqual(reference, result, msg="Reference and user result do not match on a graph with positive cycles.")


  def test_nocycle_pos(self):
    """
    Graph has no cycles
    """

    # Create a list of graphs containing 7 nodes and without cycles
    GRAPHS = nx.generators.atlas.graph_atlas_g()
    GRAPHS = list(filter(lambda g: nx.is_tree(g) and g.number_of_nodes() == 7, GRAPHS[1:]))
    
    # Select a random graph
    G = random.sample(GRAPHS, 1)[0] 

    # Generate random attributes
    edge_attr = {e: {"delay": random.randint(0, 1), "price":random.randint(0, 1)} for e in G.edges}
    nx.set_edge_attributes(G, edge_attr)

    # Write to file
    input_file = "test-samepath-nocycle.txt"
    S, T = save(G, filename=input_file)

    # Run user program
    subprocess.check_call(["./lab1.out", input_file])

    reference = int(nx.bellman_ford_path(G, S, T, "delay") == nx.bellman_ford_path(G, S, T, "price"))
    result    = self.load_result()

    self.assertEqual(reference, result, msg="Reference and user result do not match on a graph with no cycles.")
  
    
if __name__ == '__main__':

  suite = unittest.TestSuite()

  if len(sys.argv) == 2 and sys.argv[1] == "test-negativecycle":
    
    suite.addTest(unittest.defaultTestLoader.loadTestsFromTestCase(Lab1TestCycle))


  elif len(sys.argv) == 2 and sys.argv[1] == "test-cost":
    
    suite.addTest(unittest.defaultTestLoader.loadTestsFromTestCase(Lab1TestCost))
  

  elif len(sys.argv) == 2 and sys.argv[1] == "test-shortestpath":
    
    suite.addTest(unittest.defaultTestLoader.loadTestsFromTestCase(Lab1TestPath))
  

  elif len(sys.argv) == 2 and sys.argv[1] == "test-sameshortestpath":

    suite.addTest(unittest.defaultTestLoader.loadTestsFromTestCase(Lab1TestSamePath))


  elif len(sys.argv) == 2 and sys.argv[1] == "test-all":

    suite.addTest(unittest.defaultTestLoader.loadTestsFromTestCase(Lab1TestCycle))
    suite.addTest(unittest.defaultTestLoader.loadTestsFromTestCase(Lab1TestCost))
    suite.addTest(unittest.defaultTestLoader.loadTestsFromTestCase(Lab1TestPath))
    suite.addTest(unittest.defaultTestLoader.loadTestsFromTestCase(Lab1TestSamePath))

  else:
    print("No tests requested")
    sys.exit(1)


  result = unittest.TextTestRunner().run(suite)

  # Set the exit code based on the test result
  sys.exit(not result.wasSuccessful())