import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class StorageService {

  constructor() {
  }

  save(key: StorageKey, value: string) {
    sessionStorage.setItem(key, value)
  }

  get(key: StorageKey): string {
    return sessionStorage.getItem(key)
  }
}

export enum StorageKey {
  OAUTH_ACCESS_TOKEN = 'OAUTH_ACCESS_TOKEN',
  OAUTH_ID_TOKEN = 'OAUTH_ID_TOKEN'

}


