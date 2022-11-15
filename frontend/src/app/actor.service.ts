import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { observable, Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { ActorCount } from './interaces/actor';
import { ActorDocument } from './interaces/actordocument';


@Injectable({
  providedIn: 'root'
})

export class ActorService {
  actorCount : string = "http://localhost:8080/api/actors/count";
  freeSearchUrl : string = "http://localhost:8080/api/actors/freeSearch";

  constructor(private http: HttpClient) { }

  getActorCount (){
    return this.http.get<ActorCount>(this.actorCount)
  }

  searchForActors (toSearch : string) {
    return this.http.get<ActorDocument[]>(this.freeSearchUrl+"?q="+toSearch)
  }
}
