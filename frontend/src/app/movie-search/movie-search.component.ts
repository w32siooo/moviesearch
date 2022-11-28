import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MovieDocument } from '../interfaces/moviedocument';
import { MovieService } from '../movie.service';

@Component({
  selector: 'app-movie-search',
  templateUrl: './movie-search.component.html',
  styleUrls: ['./movie-search.component.css']
})
export class MovieSearchComponent implements OnInit {
  actorCount: number = 0;
  searchInput: string = '';
  movies: MovieDocument[] = [];
  errorMsg: string = '';

  constructor(private movieService : MovieService) { }

  ngOnInit(): void {
    this.movieService.getActorCount().subscribe((sub) =>
    this.actorCount = sub.count)
  }

  search(q: string): void {
    this.movieService.searchForMovies(q).subscribe((sub) => {
      this.errorMsg = ''
      this.movies = sub
    }, (err: HttpErrorResponse) => {
      if (err.status == 404) {
        this.errorMsg = err.message;
        this.movies = []
      }
    })
  }
}
