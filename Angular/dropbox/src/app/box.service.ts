import { Injectable } from '@angular/core';
import { Http, Response, RequestOptions, Headers, URLSearchParams } from '@angular/http';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import 'rxjs/Rx';
import { Observable } from 'rxjs/Rx';

@Injectable({
    providedIn: 'root'
})
export class BoxService {
    constructor(private http: Http, private httpClient: HttpClient){}

    private options = new RequestOptions({headers: new Headers({'Content-Type': 'application/json'})});
    private HttpUploadOptions = {
        headers: new HttpHeaders({ "Content-Type": "multipart/form-data" })
      }
    getFilesandFolders(path: string): Observable<any>{
        let params : URLSearchParams = new URLSearchParams();
        params.set('path', path);
        this.options.search = params;
        return this.http.get('http://localhost:8080/box/', this.options).map((res: Response) => res.json());
    }

    downloadFile(path: string): Observable<any>{
        let params : URLSearchParams = new URLSearchParams();
        params.set('path', path);
        this.options.search = params;
        return this.http.get('http://localhost:8080/box/fileDownloadUI', this.options).map((res: Response) => res.json());
          
    }

    uploadFile(formData: FormData): Observable<any>{
        return this.httpClient.post('http://localhost:8080/box/fileUpload', formData);
    }
}