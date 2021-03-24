import React from 'react';
import logo from '../logo.png';
import SearchBar from './SearchBar';


interface Props {
    setFullQuery: React.Dispatch<React.SetStateAction<string>>;
    setQuery: React.Dispatch<React.SetStateAction<string>>;
    language: string;
    setDate: React.Dispatch<React.SetStateAction<string>>;
    setCount: React.Dispatch<React.SetStateAction<number>>;
};

const Header: React.FC<Props> = ({ setFullQuery, setQuery, language, setDate, setCount }) => {
    return (
        <div style={{margin: 25}}>
            <img src={logo} alt="" style={{position: 'relative', width: 250, top: 20}} />
            <SearchBar
                setFullQuery={setFullQuery}
                setQuery={setQuery}
                language={language}
                setDate={setDate}
                setCount={setCount}
            />
        </div>
    );
};

export default Header;
