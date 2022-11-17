import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActorService } from '../actor.service';
import { ActorDocument } from '../interaces/actordocument';

@Component({
  selector: 'app-actor-search',
  templateUrl: './actor-search.component.html',
  styleUrls: ['./actor-search.component.css']
})
export class ActorSearchComponent implements OnInit {

  actorCount: number = 0;
  searchInput: string = '';
  actors: ActorDocument[] = [];
  errorMsg: string = '';

  constructor(private actorService: ActorService) { }

  ngOnInit(): void {
    this.actorService.getActorCount().subscribe((sub) =>
      this.actorCount = sub.count)
  }

  search(q: string): void {
    this.actorService.searchForActors(q).subscribe((sub) => {
      this.errorMsg = ''
      this.actors = sub
    }, (err: HttpErrorResponse) => {
      if (err.status == 404) {
        this.errorMsg = err.message;
        this.actors = []
      }
    })
  }
}
