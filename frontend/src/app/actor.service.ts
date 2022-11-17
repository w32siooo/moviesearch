import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActorCount } from './interaces/actor';
import { ActorDocument } from './interaces/actordocument';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})

export class ActorService {

  constructor(private http: HttpClient) { }

  getActorCount (){
    return this.http.get<ActorCount>(environment.baseUrl+"/actors/count")
  }

  searchForActors (toSearch : string) {
    toSearch = toSearch.trim()
    return this.http.get<ActorDocument[]>(environment.baseUrl+"/actors/freeSearch?q="+toSearch)
  }
}
