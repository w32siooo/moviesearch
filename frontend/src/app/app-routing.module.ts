import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ActorSearchComponent } from './actor-search/actor-search.component';
import { MovieSearchComponent } from './movie-search/movie-search.component';

const routes: Routes = [];

@NgModule({
  imports: [
    RouterModule.forRoot([
      { path: 'movies', component: MovieSearchComponent },
      { path: 'actors', component: ActorSearchComponent },
      { path: '**', redirectTo: 'movies' }
    ],{ useHash: true })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
