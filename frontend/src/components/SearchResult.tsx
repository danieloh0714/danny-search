import React from 'react';
import TextSnippet from './TextSnippet';


interface Props {
    query: string;
    article: any;
};

const SearchResult: React.FC<Props> = ({ query, article }) => {
    const { title, url, txt } = article;

    return (
        <div style={{margin: 20, width: '40%'}}>
            <div>
                <a href={url}>
                    {title}
                </a>
            </div>
            <div style={{fontSize: 12, color: 'gray'}}>
                {url}
            </div>
            <TextSnippet query={query} txt={txt} />
        </div>
    );
};

export default SearchResult;
