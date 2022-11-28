import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActorCount } from './interfaces/actor';
import { environment } from 'src/environments/environment';
import { MovieDocument } from './interfaces/moviedocument';

@Injectable({
  providedIn: 'root'
})
export class MovieService {

  constructor(private http: HttpClient) { }
  getActorCount (){
    return this.http.get<ActorCount>(environment.baseUrl+"/movies/count")
  }

  searchForMovies (toSearch : string) {
    toSearch = toSearch.trim()
    return this.http.get<MovieDocument[]>(environment.baseUrl+"/movies/freeSearch?q="+toSearch)
  }
}
