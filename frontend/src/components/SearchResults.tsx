import React from 'react';
import SearchResult from './SearchResult';


interface Props {
    query: string;
    articles: Array<any>;
};

const SearchResults: React.FC<Props> = ({ query, articles }) => {
    return (
        <div style={{margin: 20}}>
            {articles.map(
                (article: any) => <SearchResult query={query} article={article} />
            )}
        </div>
    );
};

export default SearchResults;
