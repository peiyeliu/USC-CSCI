import time
from bs4 import BeautifulSoup
from time import sleep
import requests
from random import randint
from html.parser import HTMLParser
import json
import csv

USER_AGENT = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36'}

bing_header = 'http://www.bing.com/search?q='
count_modifier = '&count=30'


class SearchEngine:
    @staticmethod
    def search(query, sleep=True):
        if sleep:  # Prevents loading too many pages too soon
            time.sleep(randint(10, 100))
        temp_url = '+'.join(query.split())  # for adding + between words for the query
        url = bing_header + temp_url + count_modifier
        soup = BeautifulSoup(requests.get(url, headers=USER_AGENT).text, "html.parser")
        new_results = SearchEngine.scrape_search_result(soup)
        return new_results

    @staticmethod
    def scrape_search_result(soup):
        raw_results = soup.find_all('li', attrs={'class': 'b_algo'})
        results = []
        # implement a check to get only 10 results and also check that URLs must not be duplicated
        for result in raw_results:
            if len(results) == 10:
                break
            link = result.find('a').get('href')
            if link in results:
                continue
            results.append(link)
        return results


def bing_query():
    queries_dict = dict()
    file = open('source/100QueriesSet1.txt', mode='r')
    lines = file.readlines()
    file.close()
    for line in lines:
        queries_dict[line.strip()] = SearchEngine.search(line.strip(), sleep=False)
    file = open('output/hw1.json', mode='w')
    json_str = json.dumps(queries_dict)
    file.write(json_str)
    file.close()


def single_query_test():
    query = 'how to learn java'
    result = SearchEngine.search(query)
    print(result)


def parse_json(file_path):
    file = open(file_path, mode='r')
    dict_original = json.loads(file.read())
    dict_result = dict()
    for key in dict_original:
        dict_result[key] = []
        for url in dict_original[key]:
            url = url.lower()
            url = url.removeprefix('http://')
            url = url.removeprefix('https://')
            url = url.removeprefix('www.')
            url = url.removesuffix('/')
            dict_result[key].append(url)
    return dict_result


def get_overlapping_result(key, google_arr, bing_arr):
    matched = 0
    d_square = 0
    total = min(len(google_arr), len(bing_arr))
    for i in range(len(google_arr)):
        for j in range(len(bing_arr)):
            if google_arr[i] == bing_arr[j]:
                matched += 1
                d_square += (i - j) * (i - j)
    if matched == 1:
        if d_square == 0:
            coefficient = 1
        else:
            coefficient = 0
    else:
        coefficient = 1 - 6 * d_square / (total * total * total - total)
    return [key, matched, matched / total, coefficient]


def main():
    bing_query()
    google = parse_json('source/Google_Result1.json')
    bing = parse_json('output/hw1.json')
    if len(bing) != 100:
        print("there must be 100 entries")
        return
    for key in bing:
        if len(bing[key]) < 10:
            print("the query: " + key + " only has " + str(len(bing[key])) + " results")
        if len(bing[key]) == 0:
            print("ERROR: the query: " + key + " has no results, program terminated!!!!!!!!")
    producecsv()


def producecsv():
    google = parse_json('testjsonfile/googletest.json')
    bing = parse_json('testjsonfile/bingtest.json')
    idx = 1
    csv_dict = []
    for query in google:
        key = "Query " + str(idx)
        if len(bing[query]) == 0:
            print("no result in this query")
            return
        result_tuple = get_overlapping_result(key, google[query], bing[query])
        csv_dict.append(result_tuple)
        idx += 1
    entry_count = 0
    overlap = 0
    percent_overlap = 0
    coefficient_total = 0
    with open('testjsonfile/test.csv', 'w', newline='') as csvfile:
        spamwritter = csv.writer(csvfile, delimiter=',')
        spamwritter.writerow(["Queries", " Number of Overlapping Results", " Percent Overlap", " Spearman Coefficient"])
        for res_tuple in csv_dict:
            entry_count += 1
            overlap += res_tuple[1]
            percent_overlap += res_tuple[2]
            coefficient_total += res_tuple[3]
            spamwritter.writerow(
                [res_tuple[0], " " + str(res_tuple[1]), " " + str(res_tuple[2]), " " + str(res_tuple[3])])
        spamwritter.writerow(
            ["Averages", " " + str(overlap / entry_count), " " + str(percent_overlap / entry_count), " " + str(coefficient_total / entry_count)])
        csvfile.close()


if __name__ == '__main__':
    # main()
    producecsv()
