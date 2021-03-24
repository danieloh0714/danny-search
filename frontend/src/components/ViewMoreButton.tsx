import React from 'react';
import { Button } from '@material-ui/core';


interface Props {
    setFullQuery: React.Dispatch<React.SetStateAction<string>>;
    query: string;
    count: number;
    setCount: React.Dispatch<React.SetStateAction<number>>;
    totalResults: number;
    articles: Array<any>;
};

const ViewMoreButton: React.FC<Props> = ({ setFullQuery, query, count, setCount, totalResults, articles }) => {
    const isMoreResults = articles.length < totalResults;

    const handleOnClick = () => {
        setFullQuery(`${query}&count=${count + 10}`);
        setCount(count + 10);
    };

    return (
        <Button
            variant="contained"
            color="primary"
            onClick={() => handleOnClick()}
            disabled={!isMoreResults}
            style={{marginLeft: '15%'}}
        >
            {isMoreResults ? 'View More Results' : 'All Results Loaded'}
        </Button>
    );
};

export default ViewMoreButton;
