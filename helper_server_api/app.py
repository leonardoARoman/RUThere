"""
@author: Leonardo Roman 12/06/2018
RESTful api that performs sequential API calls to Yelp.
This helper server queries restaurants to Yelp, extracts the
business ID, and performs another query to get the business reviews
by ID.
"""
from flask import Flask, request, Response, json
from yelpapi import YelpAPI
from pprint import pprint

key = 'KEY'
yelp_api = YelpAPI(key)

app = Flask(__name__)
@app.route('/',methods = ['GET'])
def api_root():
    data = {
        'hello'  : 'world',
        'number' : 3
    }
    js = json.dumps(data)
    resp = Response(js, status=200, mimetype='application/json')
    return resp
# lat = 39.3898 / lon = -74.5240
@app.route("/restaurant/<latitude>/<longitude>")
def get_address(longitude,latitude):
    # get the restaurant by given coordinates and by a radius of 100 m
    response = yelp_api.search_query(categories='Restaurants', longitude=longitude, latitude=latitude, sort_by='rating', limit=5)
    # get the reviews by name
    js = getRestaurants(response)
    # Convert to Json format response
    restaurants = Response(js, status=200, mimetype='application/json')
    return restaurants

def getRestaurants(response):
    restaurants = {}
    list = []
    dictionary = {}
    places = {}
    for alias in response['businesses']:
        restaurants['name']=alias['name']
        restaurants['phone']=alias['display_phone']
        restaurants['rating']=alias['rating']
        restaurants['coordinates']=alias['coordinates']
        restaurants['address']=alias['location']['display_address']
        id=alias['id']
        restaurants['reviews']=get_reviews(id)
        list.append(restaurants)
        restaurants = {}
    places['restaurants'] = list
    restaurants_json = json.dumps(places)
    return restaurants_json

def get_reviews(id):
    review_list = {}
    list = []
    reviews = yelp_api.reviews_query(id=id)
    for review in reviews['reviews']:
        review_list['review']=review['text']
        review_list['date']=review['time_created']
        review_list['user']=review['user']['name']
        list.append(review_list)
        review_list = {}
    return list

if __name__ == '__main__':
    app.run(debug = True)
