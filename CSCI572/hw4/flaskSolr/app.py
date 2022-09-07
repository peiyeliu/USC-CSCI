import requests
from flask import Flask, request, render_template, jsonify
import csv
import spell

app = Flask(__name__)

# please replace <myexample> by the name of core you created in Solr
url_head = "http://localhost:8983/solr/myexample/select?q="

# this is the path to the crawl folder
# in this case, all crawled pages are located in the folder called "nytimes"
id_head = "/Users/pyl/solr-7.7.3/nytimes/"

# this is where you put the file "URLtoHTML_nytimes_news.csv"
file_path = 'urlfilemap/URLtoHTML_nytimes_news.csv'

# load the url-to-html csv file and save them in a python dictionary
url_file_map = {}
with open(file_path, newline='') as csvfile:
    reader = csv.reader(csvfile)
    for line in reader:
        url_file_map[id_head + line[0]] = line[1]


@app.route('/')
def index_page():
    return render_template("index.html")


# HW5 update: autocomplete function
# When the user type word in the query box, this function will send request to solr backend
# to give autocomplete suggestions
# Reference: https://roytuts.com/autocomplete-input-suggestion-using-python-and-flask/
@app.route('/', methods=["POST"])
def search():
    res_dict = []
    term = request.form['q']

    suggest_url_head = 'http://localhost:8983/solr/myexample/suggest?q='
    suggest_url = suggest_url_head + str(term)
    res = requests.get(suggest_url).json()['suggest']['suggest'][term]['suggestions']

    for item in res:
        res_dict.append(item['term'])

    resp = jsonify(res_dict)
    resp.status_code = 200
    return resp


@app.route('/getQuery', methods=["GET"])
def get_query_result():
    print(request)
    query = request.args.get('query')
    corrected_query = spell.correction(query)
    engine = request.args.get('engine')
    if len(query) == 0 or (engine != "PageRank" and engine != 'Lucene'):
        return render_template("index.html", result=[], number=0, engine=engine,
                               total=0)

    tail = "&sort=pageRankFile%20desc" if engine == "PageRank" else ""
    url = url_head + query + tail
    res = requests.get(url).json()['response']['docs']
    total_num = requests.get(url).json()['response']['numFound']

    result_list = []
    num_of_entry = len(res)
    for i in range(num_of_entry):
        entry = res[i]
        res_entry = {"id": entry["id"] if "id" in entry else "NA",
                     "title": entry["title"][0] if "title" in entry else "NA",
                     "url": entry["og_url"][0] if "og_url" in entry else "NA",
                     "description": entry["og_description"][0] if "og_description" in entry else "NA"}
        if res_entry["url"] == "NA":
            print("here is called")
            url_id = res_entry['id']
            print(url_id)
            if url_id in url_file_map.keys():
                res_entry["url"] = url_file_map[url_id]
        result_list.append(res_entry)

    misspelled = False
    if query != corrected_query:
        misspelled = True
    return render_template("index.html", result=result_list,
                           number=len(result_list), engine=engine,
                           total=total_num, query=query, misspelled=misspelled, corrected_query=corrected_query)


if __name__ == '__main__':
    app.run()
