export interface ActorDocument{
      id : string,
      version : number,
      nconst : string,
      primaryName : string,
      birthYear : number,
      deathYear : number,
      primaryProfession : string[],
      knownForTitles : string[]
}